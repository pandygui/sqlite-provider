
package novoda.lib.sqliteprovider.migration;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doReturn;
import novoda.lib.sqliteprovider.util.RoboRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RunWith(RoboRunner.class)
public class MigrationsTest {

    @Mock
    SQLiteDatabase db;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testShouldInsertOrNotFileCorrectly() {
        Migrations migration = new Migrations();
        assertTrue("a correct file name can be inserted", migration.add("12_test.sql"));
        assertTrue("a correct file name can be inserted", migration.add("121321313_test_teste.sql"));
        assertFalse("a file name without .sql can not be inserted", migration.add("12_test"));
        assertFalse("a file name without timestamp can not be inserted", migration.add("test.sql"));

        assertThat(migration.getMigrationsFiles().size(), equalTo(2));
    }

    @Test
    public void testOrderedOfImport() {
        Migrations migration = new Migrations();
        migration.add("3_test.sql");
        migration.add("1_test.sql");
        migration.add("2_test.sql");
        migration.add("5_test.sql");
        migration.add("0_test.sql");

        assertThat(migration.getMigrationsFiles().size(), equalTo(5));
        assertThat(migration.getMigrationsFiles().first(), equalTo("0_test.sql"));
        assertThat(migration.getMigrationsFiles().last(), equalTo("5_test.sql"));
    }

    @Test
    public void testStartDate() {
        Migrations migration = new Migrations(4);
        migration.add("3_test.sql");
        migration.add("1_test.sql");
        migration.add("2_test.sql");
        migration.add("5_test.sql");
        migration.add("0_test.sql");

        assertThat(migration.getMigrationsFiles().size(), equalTo(1));
        assertThat(migration.getMigrationsFiles().last(), equalTo("5_test.sql"));
    }

    @Test
    public void setDBVersion() throws IOException {
        Context context = mock(Context.class);
        AssetManager manager = mock(AssetManager.class);

        when(context.getAssets()).thenReturn(manager);
        when(manager.list(anyString())).thenReturn(new String[] {
                "1234_test.sql", "12345_test.sql", "123456_test.sql"
        });
        
        when(manager.open(anyString())).thenReturn(
                new ByteArrayInputStream("CREATE TABLE IF NOT EXIST test(id integer);"
                        .getBytes("UTF-8")));
        
        Migrations.migrate(db, context, "sql");
        verify(db).setVersion(123456);
    }
}